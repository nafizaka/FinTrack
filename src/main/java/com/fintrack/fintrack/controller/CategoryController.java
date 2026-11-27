package com.fintrack.fintrack.controller;

import com.fintrack.fintrack.dto.CategoryForm;
import com.fintrack.fintrack.model.Category;
import com.fintrack.fintrack.model.User;
import com.fintrack.fintrack.repository.UserRepository;
import com.fintrack.fintrack.service.CategoryService;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public CategoryController(
            CategoryService categoryService,
            UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(
                        "Pengguna tidak ditemukan"
                ));
    }

    // Menampilkan daftar kategori
    @GetMapping
    public String categories(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = getLoggedInUser(userDetails);

        model.addAttribute(
                "categories",
                categoryService.getAllCategories(user.getUserId())
        );

        return "categories";
    }

    // Menampilkan form tambah kategori
    @GetMapping("/new")
    public String newCategory(Model model) {

        model.addAttribute("form", new CategoryForm());
        model.addAttribute("pageTitle", "Tambah Kategori");
        model.addAttribute("formAction", "/categories/save");

        return "category-form";
    }

    // Menyimpan kategori baru
    @PostMapping("/save")
    public String saveCategory(
            @Valid @ModelAttribute("form") CategoryForm form,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Tambah Kategori");
            model.addAttribute("formAction", "/categories/save");
            return "category-form";
        }

        try {
            categoryService.createCategory(form, user.getUserId());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Kategori berhasil ditambahkan."
            );

        } catch (IllegalArgumentException e) {
            model.addAttribute("pageTitle", "Tambah Kategori");
            model.addAttribute("formAction", "/categories/save");
            model.addAttribute("errorMessage", e.getMessage());
            return "category-form";
        }

        return "redirect:/categories";
    }

    // Menampilkan form edit kategori
    @GetMapping("/edit/{id}")
    public String editCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = getLoggedInUser(userDetails);

        Category category = categoryService.getCategory(
                id,
                user.getUserId()
        );

        CategoryForm form = new CategoryForm();
        form.setCategoryName(category.getCategoryName());
        form.setType(category.getType());

        model.addAttribute("form", form);
        model.addAttribute("categoryId", id);
        model.addAttribute("pageTitle", "Edit Kategori");
        model.addAttribute("formAction", "/categories/update/" + id);

        return "category-form";
    }

    // Memperbarui kategori
    @PostMapping("/update/{id}")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") CategoryForm form,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        if (result.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("pageTitle", "Edit Kategori");
            model.addAttribute("formAction", "/categories/update/" + id);
            return "category-form";
        }

        try {
            categoryService.updateCategory(id, form, user.getUserId());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Kategori berhasil diperbarui."
            );

        } catch (IllegalArgumentException e) {
            model.addAttribute("categoryId", id);
            model.addAttribute("pageTitle", "Edit Kategori");
            model.addAttribute("formAction", "/categories/update/" + id);
            model.addAttribute("errorMessage", e.getMessage());
            return "category-form";
        }

        return "redirect:/categories";
    }

    // Menghapus kategori
    @PostMapping("/delete/{id}")
    public String deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(userDetails);

        try {
            categoryService.deleteCategory(id, user.getUserId());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Kategori berhasil dihapus."
            );

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Kategori tidak dapat dihapus karena masih digunakan oleh transaksi."
            );
        }

        return "redirect:/categories";
    }
}
