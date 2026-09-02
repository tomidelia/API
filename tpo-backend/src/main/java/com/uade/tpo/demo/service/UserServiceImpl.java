package com.uade.tpo.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.RoleRequest;
import com.uade.tpo.demo.entity.dto.UserResponse;
import com.uade.tpo.demo.exceptions.ForbiddenActionException;
import com.uade.tpo.demo.exceptions.UserNotFoundException;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(PageRequest pageRequest) {
        return userRepository.findAll(pageRequest).map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) throws UserNotFoundException {
        return UserResponse.from(findUser(userId));
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long userId, Long adminId, RoleRequest roleRequest)
            throws UserNotFoundException, ForbiddenActionException {

        // Si un admin pudiera bajarse el rol a si mismo, la tienda podria
        // quedarse sin ninguna cuenta capaz de administrar productos.
        if (userId.equals(adminId))
            throw new ForbiddenActionException("Un administrador no puede cambiar su propio rol");

        User user = findUser(userId);
        user.setRole(roleRequest.getRole());

        return UserResponse.from(userRepository.save(user));
    }

    private User findUser(Long userId) throws UserNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No existe el usuario con id " + userId));
    }
}
