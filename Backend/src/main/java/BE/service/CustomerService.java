package BE.service;

import BE.entity.Customer;
import BE.model.DTO.CustomerDTO;
import BE.model.response.CustomerResponse;
import BE.repository.CustomerRepository;
import BE.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        CustomerResponse customerResponse = new CustomerResponse();
        modelMapper.map(customer, customerResponse);

        return customerResponse;
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        if (customerRepository.findByPhoneAndCustomerIDNot(dto.getPhone(), id).isPresent()){
            throw new IllegalArgumentException("Phone number already exists");
        }

        if (customerRepository.findByEmailAndCustomerIDNot(dto.getEmail(), id).isPresent()){
            throw new IllegalArgumentException("Email already exists");
        }

        modelMapper.map(dto, customer);

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()){
            customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Customer updatedCustomer = customerRepository.save(customer);
        CustomerResponse customerResponse = new CustomerResponse();
        modelMapper.map(updatedCustomer, customerResponse);

        return customerResponse;
    }

    @Transactional
    public void deleteCustomer(Long id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        customer.setStatus(false);

        userRepository.findByRefIdAndRefType(id, "CUSTOMER")
                .ifPresent(user -> {
                    user.setStatus(false);
                    userRepository.save(user);
                });

        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomer(){
        return customerRepository.findByStatus(true)
                .stream()
                .map(customer -> {
                    CustomerResponse response = new CustomerResponse();
                    modelMapper.map(customer, response);
                    return response;
                })
                .toList();
    }
}
