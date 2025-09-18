package BE.service;


import BE.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import BE.repository.AuthenticationRepository;

import java.util.List;

@Service
public class AuthenticationService {
    //xử lý logic của controller đưa qua
    @Autowired
    AuthenticationRepository authenticationRepository;

    public Customer register(Customer account){
        //Xử lý logic của register

        //lưu vào DB
        return authenticationRepository.save(account);
    }

    public List<Customer> getAllAccount(){
        List<Customer> accounts = authenticationRepository.findAll();
        return accounts;
    }
}
