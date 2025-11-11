package com.finx.allocationreallocationservice.client;

import com.finx.allocationreallocationservice.client.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "access-management-service", path = "/api/v1/users")
public interface UserManagementClient {

    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/by-geography")
    List<UserDTO> getUsersByGeography(@RequestParam("geographies") List<String> geographies);

    @GetMapping("/active-agents")
    List<UserDTO> getActiveAgents();
}
