package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.RoleDTO;
import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.entities.Role;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.RoleRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;



    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        Page<User> userList = repository.findAll(pageable);
        Page<UserDTO> dtoList = userList.map(x -> new UserDTO(x));
        return dtoList;
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id){
        Optional<User> user = repository.findById(id);
        Optional<UserDTO> userDTO = Optional.of(new UserDTO(user.get()));
        return userDTO.get();
    }

    @Transactional
    public UserDTO insert(UserInsertDTO requestUser){
        User user = new User();
        copyDtoToEntity(requestUser,user);
        user.setPassword(passwordEncoder.encode(requestUser.getPassword()));
        repository.saveAndFlush(user);
        return new UserDTO(user);
    }

    @Transactional
    public UserDTO update(Long id, UserDTO requestUser){
        try {
            User entity = repository.getOne(id);
            copyDtoToEntity(requestUser, entity);
            return new UserDTO(entity);
        }catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    public void deleteById(Long id){
        try {
            repository.deleteById(id);
        }catch (DatabaseException e){
            throw new DatabaseException("Id not found " + id);
        }catch (DataIntegrityViolationException e ){
            throw new DatabaseException("Integrity violation");
        }
    }






    private void copyDtoToEntity(UserDTO dto, User entity){
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());

        entity.getRoles().clear();
        for (RoleDTO roleDTO : dto.getRoles()){
            Role role = roleRepository.getOne(roleDTO.getId());
            entity.getRoles().add(role);
        }
    }




}
