package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    private final AuthUtils authUtils;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO){
        User user  = authUtils.loggedInUser();
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO ,user);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(){
//        User user = authUtils.loggedInUser();
        List<AddressDTO> dtos = addressService.getAddressList();
        return new ResponseEntity<>(dtos,HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddresseById(@PathVariable Long addressId){
//        User user = authUtils.loggedInUser();
        AddressDTO dto = addressService.getAddressesById(addressId);

        return new ResponseEntity<>(dto,HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddress(){
        User user = authUtils.loggedInUser();
        List<AddressDTO> dto = addressService.getUserAddress(user);
        return new ResponseEntity<>(dto,HttpStatus.OK);
    }

    @PutMapping(("/addresses/{addressId}"))
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId,@RequestBody AddressDTO addressDTO){
        AddressDTO updatedAddress = addressService.updateAddress(addressId,addressDTO);
        return  new ResponseEntity<>(updatedAddress,HttpStatus.OK);
    }
    @DeleteMapping(("/addresses/{addressId}"))
    public ResponseEntity<String> deleteAddressById(@PathVariable Long addressId){
       String msg = addressService.deleteAddress(addressId);
        return  new ResponseEntity<>(msg,HttpStatus.OK);
    }




}
