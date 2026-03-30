package com.example.demo.controller;

import com.example.demo.model.CartItem;
import com.example.demo.model.Order;
import com.example.demo.model.Product;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    private final ProductService productService;
    private final OrderService orderService;

    public CartController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("error", "Quantity must be at least 1.");
            return "redirect:/products";
        }

        List<CartItem> cart = getCart(session);

        // Calculate how many are already in cart
        int currentInCart = 0;
        for (CartItem item : cart) {
            if (item.getProductId().equals(productId)) {
                currentInCart = item.getQuantity();
                break;
            }
        }

        // Check stock availability
        if (currentInCart + quantity > product.getQuantity()) {
            redirectAttributes.addFlashAttribute("error",
                    "Not enough stock for \"" + product.getName() + "\". Available: "
                    + product.getQuantity() + ", in cart: " + currentInCart + ".");
            return "redirect:/products";
        }

        // Add or update cart item
        for (CartItem item : cart) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                redirectAttributes.addFlashAttribute("success",
                        "Updated \"" + product.getName() + "\" quantity to " + item.getQuantity() + ".");
                return "redirect:/products";
            }
        }

        cart.add(new CartItem(product.getId(), product.getName(), product.getPrice(), quantity));
        redirectAttributes.addFlashAttribute("success",
                "Added \"" + product.getName() + "\" to cart.");
        return "redirect:/products";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = getCart(session);
        double totalAmount = cart.stream().mapToDouble(CartItem::getTotal).sum();
        model.addAttribute("cartItems", cart);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("cartSize", cart.size());
        return "cart";
    }

    @PostMapping("/cart/update/{productId}")
    public String updateQuantity(@PathVariable Long productId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        List<CartItem> cart = getCart(session);

        if (quantity <= 0) {
            cart.removeIf(item -> item.getProductId().equals(productId));
            redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
            return "redirect:/cart";
        }

        // Validate stock
        Product product = productService.getProductById(productId).orElse(null);
        if (product != null && quantity > product.getQuantity()) {
            redirectAttributes.addFlashAttribute("error",
                    "Not enough stock for \"" + product.getName() + "\". Available: " + product.getQuantity() + ".");
            return "redirect:/cart";
        }

        for (CartItem item : cart) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }

        redirectAttributes.addFlashAttribute("success", "Cart updated.");
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getProductId().equals(productId));
        redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
        return "redirect:/cart";
    }

    @GetMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("cart");
        redirectAttributes.addFlashAttribute("success", "Cart cleared.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        List<CartItem> cart = getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty.");
            return "redirect:/cart";
        }

        // Validate stock for all items before checkout
        for (CartItem item : cart) {
            Product product = productService.getProductById(item.getProductId()).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error",
                        "Product \"" + item.getProductName() + "\" is no longer available.");
                return "redirect:/cart";
            }
            if (item.getQuantity() > product.getQuantity()) {
                redirectAttributes.addFlashAttribute("error",
                        "Not enough stock for \"" + product.getName()
                        + "\". Available: " + product.getQuantity()
                        + ", requested: " + item.getQuantity() + ".");
                return "redirect:/cart";
            }
        }

        String username = authentication.getName();
        Order order = orderService.createOrder(username, cart);

        // Deduct stock
        for (CartItem item : cart) {
            Product product = productService.getProductById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productService.saveProduct(product);
            }
        }

        // Clear cart after successful checkout
        session.removeAttribute("cart");

        model.addAttribute("order", order);
        return "order-success";
    }

    // Order history
    @GetMapping("/orders")
    public String orderHistory(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<Order> orders = orderService.getOrdersByUsername(username);
        model.addAttribute("orders", orders);
        return "orders";
    }
}
