package com.frontend.config;

import com.frontend.model.SocialLink;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("footerAboutTitle", "About");
        model.addAttribute("footerAboutText", "Your trusted partner since 2025.");
        model.addAttribute("contactPhone", "+1 234 567 8900");
        model.addAttribute("contactPhoneLink", "tel:+12345678900");
        model.addAttribute("contactEmail", "info@servicexpress.com");
        model.addAttribute("contactEmailLink", "mailto:info@servicexpress.com");
        model.addAttribute("contactAddress", "123 Service St, Auto City");
        model.addAttribute("footerCopyright", "© 2025 ServiceXpress. All rights reserved.");
        List<SocialLink> socialLinks = Arrays.asList(
            new SocialLink("https://facebook.com", "fab fa-facebook-f"),
            new SocialLink("https://twitter.com", "fab fa-twitter"),
            new SocialLink("https://instagram.com", "fab fa-instagram")
        );
        model.addAttribute("socialLinks", socialLinks);
    }
}
