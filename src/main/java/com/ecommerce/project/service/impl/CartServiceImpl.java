package com.ecommerce.project.service.impl;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDto;
import com.ecommerce.project.Repository.CartItemRepository;
import com.ecommerce.project.Repository.CartRepository;
import com.ecommerce.project.Repository.ProductRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final AuthUtils authUtil;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;
    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        //find existing cart or create one
        Cart cart = createCart();
        //retrieve product details
        Product product = productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));
        //perform validations
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(),productId);

        if (cartItem != null){
            throw new APIException("Product " + product.getProductName() + " already exists in cart !");
        }
        if (product.getQuantity() == 0){
            throw new APIException(product.getProductName()+" is not available");
        }
        if (product.getQuantity() < quantity){
            throw  new APIException("Please,make an order of the "+ product.getProductName()+" less than or equal to the quantity of product " + product.getQuantity() );
        }
        //create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        cart.getCartItems().add(newCartItem);

        //save cart item
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());//here we are not decreasing quantity because user haven't order yet

        cart.setTotalPrice(cart.getTotalPrice() + (quantity * product.getSpecialPrice()));
        cartRepository.save(cart);

        //return updated cart

        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

        List<CartItem> cartItemsList = cart.getCartItems();
        Stream<ProductDto> productStream = cartItemsList.stream().map(item->{
            ProductDto map = modelMapper.map(item.getProduct(),ProductDto.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

       cartDTO.setProductDtos(productStream.toList());
       return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()){
            throw new APIException("No Cart exists !!");
        }
        List<CartDTO> dtos = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
            List<ProductDto> productDtoList = cart.getCartItems().stream().map(cartItem -> {
                ProductDto productDto = modelMapper.map(cartItem.getProduct(),ProductDto.class);
                productDto.setQuantity(cartItem.getQuantity());
                System.out.println("quantity in cart" + cartItem.getQuantity());
                return productDto;
            }).toList();
            cartDTO.setProductDtos(productDtoList);
            return cartDTO;
        }).toList();
        return dtos;
    }

    @Override
        public CartDTO getCart(String emailId, Long cartId) {
       Cart cart = cartRepository.findCartByEmailAndCartId(emailId,cartId);
       if (cart == null){
           throw  new ResourceNotFoundException("Cart","cartId",cartId);
       }
       CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
       cart.getCartItems().forEach(c->c.getProduct().setQuantity(c.getQuantity()));
       List<ProductDto> productDtoList = cart.getCartItems().stream().map(p->modelMapper.map(p.getProduct(),ProductDto.class)).toList();


       cartDTO.setProductDtos(productDtoList);
       return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId).orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));

        Product product = productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));
        if (product.getQuantity() == 0){
            throw new APIException("Product "+product.getProductName()+" is not available");
        }
        if (product.getQuantity() < quantity){
            throw  new APIException("Please,make an order of the "+ product.getProductName()+" less than or equal to the quantity of product " + product.getQuantity() );
        }
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if (cartItem == null){
            throw new APIException("Product " + product.getProductName()+ " not available in cart !!!");
        }

        int newQuantity =  cartItem.getQuantity()+quantity;
        if (newQuantity < 0){
            throw new APIException("The Resulting quantity cannot be negative");
        }
        if (newQuantity == 0){
            deleteProductFromCart(cartId,productId);
        }else{
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity()+quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice()+(cartItem.getProductPrice()*quantity));
            cartRepository.save(cart);
        }
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        if (updatedCartItem.getQuantity() == 0 ){
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }
        CartDTO cartDTO =modelMapper.map(cart,CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDto> productDtoStream = cartItems.stream().map(item->{
            ProductDto prd = modelMapper.map(item.getProduct(),ProductDto.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });

        cartDTO.setProductDtos(productDtoStream.toList());
        return cartDTO;
    }
    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);

        if (cartItem == null){
            throw new ResourceNotFoundException("Product","productId",productId);
        }

        cart.setTotalPrice(cart.getTotalPrice()-(cartItem.getProductPrice()*cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);
        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart" ;
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId).orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));

        Product product = productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);

        if (cartItem == null){
            throw new APIException("Product " +product.getProductName()+ " not available");
        }
        double cartPrice  = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice()+cartItem.getQuantity()));
        cartItem = cartItemRepository.save(cartItem);
    }

    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart != null){
            return userCart;
        }
        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart = cartRepository.save(cart);

        return newCart;
    }
}
