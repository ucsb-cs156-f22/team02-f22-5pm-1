package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "Recommendation")
@RequestMapping("/api/recommendation")
@RestController
@Slf4j
public class RecommendationController extends ApiController {
    @Autowired
    RecommendationRepository recommendationRepository;

    @ApiOperation(value = "Get a recommendation by ID")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Recommendation getById(
        @ApiParam("id") @RequestParam Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recommendation.class, id));
        
        return recommendation;
    }
}
