package cm.digitrans.api.crm.controller;

import cm.digitrans.api.crm.entity.Client;
import cm.digitrans.api.crm.service.CrmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/crm/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CrmController {

    private final CrmService crmService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Client>> getAll() {
        return ResponseEntity.ok(crmService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Client> create(@RequestBody Client client) {
        return ResponseEntity.ok(crmService.save(client));
    }
}
