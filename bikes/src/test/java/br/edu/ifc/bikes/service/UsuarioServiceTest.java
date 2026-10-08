package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.dto.UsuarioSenhaDTO;
import br.edu.ifc.bikes.dto.mapper.UsuarioMapper;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final UsuarioMapper usuarioMapper = mock(UsuarioMapper.class);
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, usuarioMapper);
    }

    @Test
    void deveRejeitarSenhaAtualIncorreta() {
        Usuario usuario = usuarioComSenha("123456");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(PasswordInvalidException.class, () -> usuarioService.updatePassword(
                1L, new UsuarioSenhaDTO("654321", "abcdef", "abcdef")));

        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void deveRejeitarConfirmacaoDiferenteDaNovaSenha() {
        Usuario usuario = usuarioComSenha("123456");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(PasswordInvalidException.class, () -> usuarioService.updatePassword(
                1L, new UsuarioSenhaDTO("123456", "abcdef", "fedcba")));

        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void deveAtualizarSenhaQuandoDadosForemValidos() {
        Usuario usuario = usuarioComSenha("123456");
        UsuarioResponseDTO resposta = new UsuarioResponseDTO(1L, "usuario@ifc.edu.br", "CLIENTE");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(resposta);

        UsuarioResponseDTO resultado = usuarioService.updatePassword(
                1L, new UsuarioSenhaDTO("123456", "abcdef", "abcdef"));

        assertEquals("abcdef", usuario.getPassword());
        assertEquals(resposta, resultado);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveInformarQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.updatePassword(
                1L, new UsuarioSenhaDTO("123456", "abcdef", "abcdef")));

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any(Usuario.class));
    }

    private Usuario usuarioComSenha(String senha) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setPassword(senha);
        return usuario;
    }
}