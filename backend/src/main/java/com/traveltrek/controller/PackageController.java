package com.traveltrek.controller;

import com.traveltrek.dto.PackageRequest;
import com.traveltrek.entity.TravelPackage;
import com.traveltrek.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
@RestController
@RequestMapping("/api/v1/packages")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342",
        "https://traveltrek-dun.vercel.app"
})
public class PackageController {
    private final PackageService packageService;
    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }
    // GET /api/v1/packages?destination=goa&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<TravelPackage>>searchPackages(
            @RequestParam(required=false) String destination,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size) {
        Pageable pageable=PageRequest.of(page,size);
        return ResponseEntity.ok(packageService.searchPackages(destination,pageable));
    }
    // GET /api/v1/packages/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TravelPackage>getPackage(@PathVariable Long id) {
        return ResponseEntity.ok(packageService.getPackageById(id));
    }
    // POST /api/v1/packages
    @PostMapping
    public ResponseEntity<TravelPackage>createPackage(@Valid @RequestBody PackageRequest request) {
        TravelPackage created=packageService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    // PUT /api/v1/packages/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TravelPackage>updatePackage(@PathVariable Long id,@Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(packageService.updatePackage(id, request));
    }
    // DELETE /api/v1/packages/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
