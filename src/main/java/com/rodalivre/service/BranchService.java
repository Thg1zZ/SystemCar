package com.rodalivre.service;

import com.rodalivre.api.dto.request.BranchRequest;
import com.rodalivre.api.dto.response.BranchResponse;
import com.rodalivre.domain.entity.Branch;
import com.rodalivre.exception.LocadoraException;
import com.rodalivre.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(BranchResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new LocadoraException("Filial não encontrada"));
        return BranchResponse.fromEntity(branch);
    }

    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        if (branchRepository.findByName(request.getName()).isPresent()) {
            throw new LocadoraException("Já existe uma filial com este nome.");
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .active(true)
                .build();

        Branch savedBranch = branchRepository.save(branch);
        return BranchResponse.fromEntity(savedBranch);
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new LocadoraException("Filial não encontrada"));

        branch.setName(request.getName());
        branch.setStreet(request.getStreet());
        branch.setCity(request.getCity());
        branch.setState(request.getState());
        branch.setZipCode(request.getZipCode());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());
        branch.setPhone(request.getPhone());

        Branch updatedBranch = branchRepository.save(branch);
        return BranchResponse.fromEntity(updatedBranch);
    }
}
