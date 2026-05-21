// SPDX-License-Identifier: MIT
pragma solidity ^0.8.17;

/**
 * @title Traceability
 * @dev Contrat simplifié pour la traçabilité des mouvements de marchandises
 */
contract Traceability {
    struct Movement {
        string code;           // code de traçabilité du lot
        string ancienStatut;
        string nouveauStatut;
        string localisation;
        address agent;         // adresse Ethereum de l'agent
        uint256 timestamp;
        string remarques;
    }

    // mapping du code de traçabilité vers l'historique des mouvements
    mapping(string => Movement[]) private history;

    // Events pour permettre aux services off-chain de réagir
    event MovementRecorded(string indexed code, uint256 index, address indexed agent, uint256 timestamp);

    /**
     * @dev Enregistrer un mouvement pour un code donné
     */
    function recordMovement(
        string memory code,
        string memory ancienStatut,
        string memory nouveauStatut,
        string memory localisation,
        string memory remarques
    ) public {
        Movement memory m = Movement({
            code: code,
            ancienStatut: ancienStatut,
            nouveauStatut: nouveauStatut,
            localisation: localisation,
            agent: msg.sender,
            timestamp: block.timestamp,
            remarques: remarques
        });

        history[code].push(m);
        emit MovementRecorded(code, history[code].length - 1, msg.sender, block.timestamp);
    }

    /**
     * @dev Récupérer l'historique pour un code
     */
    function getHistory(string memory code) public view returns (Movement[] memory) {
        return history[code];
    }
}
