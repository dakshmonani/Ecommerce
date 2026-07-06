package com.ecommerce.project.service.impl;

import com.ecommerce.project.Repository.AddressRepository;
import com.ecommerce.project.Repository.UserRepository;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private  final ModelMapper modelMapper;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO,Address.class);

        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUser(user);
        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddressList() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream().map(address -> modelMapper.map(address,AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO getAddressesById(Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(()->new ResourceNotFoundException("Address","AddressId",addressId));

       return modelMapper.map(address,AddressDTO.class);

    }

    @Override
    public List<AddressDTO> getUserAddress(User user) {

        List<Address> addresses = user.getAddresses();
        return addresses.stream().map(address -> modelMapper.map(address,AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addressFromDb = addressRepository.findById(addressId).orElseThrow(()->new ResourceNotFoundException("Address","AddressId",addressId));

        addressFromDb.setCity(addressDTO.getCity());
        addressFromDb.setPincode(addressDTO.getPincode());
        addressFromDb.setCountry(addressDTO.getCountry());
        addressFromDb.setState(addressDTO.getState());
        addressFromDb.setStreet(addressDTO.getStreet());
        addressFromDb.setBuildingName(addressDTO.getBuildingName());


        Address updatedAddress = addressRepository.save(addressFromDb);
        User user = addressFromDb.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);


        return modelMapper.map(updatedAddress,AddressDTO.class);


    }

    @Override
    public String deleteAddress(Long addressId) {
        Address addressFromDb = addressRepository.findById(addressId).orElseThrow(()->new ResourceNotFoundException("Address","AddressId",addressId));


        User user = addressFromDb.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.delete(addressFromDb);


        return "Address deleted successfully with addressId " + addressId;

    }
}
