package cm.digitrans.api.erp.controller;

import cm.digitrans.api.erp.entity.OrdreFabrication;
import cm.digitrans.api.erp.service.ErpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/erp/fabrications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ErpController {

    private final ErpService erpService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<OrdreFabrication>> getAll() {
        return ResponseEntity.ok(erpService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<OrdreFabrication> create(@RequestBody OrdreFabrication order) {
        return ResponseEntity.ok(erpService.save(order));
    }
}
