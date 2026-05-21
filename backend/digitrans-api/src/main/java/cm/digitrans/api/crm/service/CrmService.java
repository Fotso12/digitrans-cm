package cm.digitrans.api.crm.service;

import cm.digitrans.api.crm.entity.Client;
import cm.digitrans.api.crm.repository.ClientRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrmService {

    private final ClientRepository clientRepository;

    @PostConstruct
    public void seedClients() {
        if (clientRepository.count() == 0) {
            log.info("CRM vide. Insertion de clients de test...");
            clientRepository.save(Client.builder().nom("SavoirManger Douala").email("douala@savoirmanger.cm").telephone("+237 677889900").pointsFidelite(1500).build());
            clientRepository.save(Client.builder().nom("SavoirManger Yaoundé").email("yaounde@savoirmanger.cm").telephone("+237 699887766").pointsFidelite(2300).build());
            clientRepository.save(Client.builder().nom("ChocoCamer S.A.").email("info@chococamer.cm").telephone("+237 233445566").pointsFidelite(500).build());
            log.info("Clients de test insérés avec succès !");
        }
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client save(Client client) {
        if (client.getPointsFidelite() == null) {
            client.setPointsFidelite(0);
        }
        return clientRepository.save(client);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Client> clients = clientRepository.findAll();
        stats.put("totalClients", clients.size());

        double averageFidelity = clients.stream()
                .mapToInt(Client::getPointsFidelite)
                .average()
                .orElse(0.0);
        stats.put("averageFidelityPoints", averageFidelity);

        return stats;
    }
}
