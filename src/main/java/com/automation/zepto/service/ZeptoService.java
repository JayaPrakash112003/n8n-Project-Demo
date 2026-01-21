package com.automation.zepto.service;

import com.automation.zepto.dto.OrderRequest;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class ZeptoService {

    public String executeOrder(OrderRequest request) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)); // Headless for Render using .setHeadless(true)

            // Context might need storage state for login persistence
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            System.out.println("Navigating to Zepto...");
            page.navigate("https://www.zepto.com/");

            // TODO: Handle Login logic here?
            // Zepto usually requires phone number + OTP.
            // If the user is not logged in, we can't really proceed to address/checkout.
            // For now, implementing the search and add flow assuming we can get past this
            // or just to demonstrate.

            // Handle multiple products
            for (String productName : request.getProducts()) {
                System.out.println("Searching for: " + productName);
                page.getByPlaceholder("Search for...").fill(productName);
                page.getByPlaceholder("Search for...").press("Enter");
                page.waitForTimeout(2000); // Wait for results

                // Add first item if available
                System.out.println("Adding " + productName + " to cart...");
                // Placeholder selector - needs verification
                // page.locator("button:has-text('Add')").first().click();

                // Clear search or navigate home to search next?
                // For safety, might be better to just clear search input
                page.getByPlaceholder("Search for...").fill("");
            }

            // Go to Cart
            // page.locator("[aria-label='Cart']").click();

            // Proceed to Pay

            // Select UPI
            // page.locator("text=UPI").click();
            // page.getByPlaceholder("Enter UPI ID").fill(request.getUpiId());
            // page.locator("button:has-text('Verify')").click();

            // Final Pay
            // page.locator("button:has-text('Pay')").click();

            return "Order process initiated for products: " + request.getProducts()
                    + ". (Note: Actual selectors need verification)";
        }
    }
}
