package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.model.Chamado;
import com.murilo.helpdesk.model.enums.Status;
import com.murilo.helpdesk.service.ChamadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @GetMapping("/{id}")
    public ResponseEntity<Chamado> findById(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Chamado>> findAll() {
        return ResponseEntity.ok(chamadoService.findAll());
    }

    @PostMapping
    public ResponseEntity<Chamado> create(@RequestBody Chamado obj) {
        Chamado newObj = chamadoService.create(obj);
        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}").buildAndExpand(newObj.getId()).toUri();
        return ResponseEntity.created(uri).body(newObj);
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<Chamado> updateStatus(
            @PathVariable Long id, @PathVariable Status status) {
        Chamado updated = chamadoService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
