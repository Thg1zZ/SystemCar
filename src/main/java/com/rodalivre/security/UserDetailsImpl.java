package com.rodalivre.security;

import com.rodalivre.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private UUID id;
    private String fullName;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean active;

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserDetailsImpl(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities,
                user.getActive()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // DECISÃO DE DESIGN (MVP): a entidade User não possui campo de expiração de conta.
        // Alterar quando o campo 'accountExpirationDate' for adicionado à entidade User.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // DECISÃO DE DESIGN (MVP): a entidade User não possui campo de bloqueio de conta.
        // Alterar quando o campo 'lockedUntil' for adicionado à entidade User.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // DECISÃO DE DESIGN (MVP): expiração de credenciais não implementada.
        // Alterar quando política de rotação de senha for adicionada.
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
