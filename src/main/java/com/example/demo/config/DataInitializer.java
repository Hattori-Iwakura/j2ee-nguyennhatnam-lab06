package com.example.demo.config;

import com.example.demo.model.Account;
import com.example.demo.model.Product;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, AccountRepository accountRepository,
                           ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create roles if not exist
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));

        // Create default admin account if not exist
        if (!accountRepository.existsByUsername("admin")) {
            Account admin = new Account("admin", passwordEncoder.encode("admin123"), "admin@techshop.com", "Administrator");
            admin.getRoles().add(adminRole);
            admin.getRoles().add(userRole);
            accountRepository.save(admin);
        }

        // Create default user account if not exist
        if (!accountRepository.existsByUsername("user")) {
            Account user = new Account("user", passwordEncoder.encode("user123"), "user@techshop.com", "Normal User");
            user.getRoles().add(userRole);
            accountRepository.save(user);
        }

        // Seed products if empty
        if (productRepository.count() == 0) {
            productRepository.save(new Product("iPhone 15 Pro Max", "Apple", "Phone", 34990000, 50, "Apple iPhone 15 Pro Max 256GB", "https://cdn2.cellphones.com.vn/x/media/catalog/product/i/p/iphone-15-pro-max_3.png"));
            productRepository.save(new Product("iPhone 15", "Apple", "Phone", 22990000, 80, "Apple iPhone 15 128GB", "https://cdn2.cellphones.com.vn/x/media/catalog/product/i/p/iphone-15_1.png"));
            productRepository.save(new Product("Samsung Galaxy S24 Ultra", "Samsung", "Phone", 31990000, 40, "Samsung Galaxy S24 Ultra 256GB", "https://cdn2.cellphones.com.vn/x/media/catalog/product/s/m/sm-s928_galaxys24ultra_devicefront_titaniumblack_dm.png"));
            productRepository.save(new Product("Samsung Galaxy A55", "Samsung", "Phone", 9990000, 100, "Samsung Galaxy A55 5G 128GB", null));
            productRepository.save(new Product("Xiaomi 14 Ultra", "Xiaomi", "Phone", 23990000, 30, "Xiaomi 14 Ultra 512GB", null));

            productRepository.save(new Product("MacBook Pro 14 M3", "Apple", "Laptop", 49990000, 25, "Apple MacBook Pro 14 inch M3 chip 16GB RAM", "https://cdn2.cellphones.com.vn/x/media/catalog/product/m/a/macbook-pro-14-m3.png"));
            productRepository.save(new Product("MacBook Air M2", "Apple", "Laptop", 27990000, 35, "Apple MacBook Air 13 inch M2 chip 8GB RAM", null));
            productRepository.save(new Product("Dell XPS 15", "Dell", "Laptop", 42990000, 15, "Dell XPS 15 Intel Core i7 32GB RAM", null));
            productRepository.save(new Product("ASUS ROG Strix G16", "ASUS", "Laptop", 37990000, 20, "ASUS ROG Strix G16 RTX 4060 16GB RAM", null));
            productRepository.save(new Product("Lenovo ThinkPad X1 Carbon", "Lenovo", "Laptop", 35990000, 18, "Lenovo ThinkPad X1 Carbon Gen 11", null));

            productRepository.save(new Product("iPad Pro M2 12.9", "Apple", "Tablet", 28990000, 30, "Apple iPad Pro 12.9 inch M2 chip 128GB", null));
            productRepository.save(new Product("iPad Air M1", "Apple", "Tablet", 16990000, 45, "Apple iPad Air 10.9 inch M1 chip 64GB", null));
            productRepository.save(new Product("Samsung Galaxy Tab S9", "Samsung", "Tablet", 19990000, 25, "Samsung Galaxy Tab S9 128GB", null));

            productRepository.save(new Product("AirPods Pro 2", "Apple", "Headphone", 6790000, 100, "Apple AirPods Pro 2nd Gen USB-C", null));
            productRepository.save(new Product("Sony WH-1000XM5", "Sony", "Headphone", 8490000, 40, "Sony WH-1000XM5 Wireless Noise Cancelling", null));
            productRepository.save(new Product("Samsung Galaxy Buds2 Pro", "Samsung", "Headphone", 4490000, 60, "Samsung Galaxy Buds2 Pro ANC", null));

            productRepository.save(new Product("Apple Watch Series 9", "Apple", "Smartwatch", 11990000, 35, "Apple Watch Series 9 GPS 45mm", null));
            productRepository.save(new Product("Samsung Galaxy Watch 6", "Samsung", "Smartwatch", 7490000, 40, "Samsung Galaxy Watch 6 Classic 47mm", null));

            productRepository.save(new Product("Apple MagSafe Charger", "Apple", "Accessory", 1190000, 200, "Apple MagSafe Wireless Charger", null));
            productRepository.save(new Product("Samsung 25W Charger", "Samsung", "Accessory", 490000, 150, "Samsung 25W USB-C Fast Charger", null));
        }
    }
}
