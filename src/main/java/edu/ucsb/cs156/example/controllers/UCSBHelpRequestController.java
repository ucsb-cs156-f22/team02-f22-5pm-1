package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBHelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBHelpRequestRepository;
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

@Api(description = "UCSBHelpRequest")
@RequestMapping("/api/ucsbhelprequest")
@RestController
@Slf4j
public class UCSBHelpRequestController extends ApiController {

    @Autowired
    UCSBHelpRequestRepository ucsbHelpRequestRepository;

    @ApiOperation(value = "List all ucsb help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBHelpRequest> allUCSBHelpRequest() {
        Iterable<UCSBHelpRequest> help_requests = ucsbHelpRequestRepository.findAll();
        return help_requests;
    }

    @ApiOperation(value = "Get a single help request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBHelpRequest getById(
            @ApiParam("id") @RequestParam Long id) {
        UCSBHelpRequest ucsbHelpRequest = ucsbHelpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBHelpRequest.class, id));

        return ucsbHelpRequest;
    }
    
    @ApiOperation(value = "Delete a UCSBHelpRequest")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBHelpRequest(
            @ApiParam("id") @RequestParam Long id) {
        UCSBHelpRequest ucsbHelpRequest = ucsbHelpRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBHelpRequest.class, id));

                ucsbHelpRequestRepository.delete(ucsbHelpRequest);
        return genericMessage("UCSBHelpRequest with id %s deleted".formatted(id));
    }
}
