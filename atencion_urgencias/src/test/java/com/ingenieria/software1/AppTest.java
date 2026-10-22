package com.ingenieria.software1;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.ingenieria.software1.model.Empleado;
import com.ingenieria.software1.model.RolEmpleado;
import com.ingenieria.software1.service.DatabaseService;
import com.ingenieria.software1.service.UsuarioService;
import org.junit.Test;

import java.sql.Connection;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void testDatabaseConnectionAndAuthentication() throws Exception
    {
        try (Connection conn = DatabaseService.getInstance().getConnection()) {
            assertNotNull("La conexión a MariaDB debe ser exitosa", conn);
            assertTrue("La conexión debe ser válida", conn.isValid(2));
        }

        UsuarioService usuarioService = UsuarioService.getInstance();
        Empleado admin = usuarioService.autenticar("admin", "1234");
        assertNotNull("La autenticación de admin debe ser exitosa", admin);
        assertTrue("El rol debe ser ADMIN", admin.getRol() == RolEmpleado.ADMIN);
    }
}
