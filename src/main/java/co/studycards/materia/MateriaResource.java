package co.studycards.materia;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/materias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MateriaResource {

    /**
     * Listar todas las materias (útil para verificar que la creación funcionó).
     * GET /materias
     */
    @GET
    public List<Materia> listar() {
        return Materia.listAll();
    }

    /**
     * HU001: crear una materia con nombre (obligatorio), descripción y color (opcionales).
     * POST /materias
     */
    @POST
    @Transactional
    public Response crear(@Valid MateriaRequest request) {
        Materia materia = new Materia(request.nombre, request.descripcion, request.color);
        materia.persist();
        return Response.status(Response.Status.CREATED).entity(materia).build();
    }

    /**
     * DTO de entrada: separa lo que el cliente puede enviar de la entidad real,
     * para que no pueda mandar un "id" propio, por ejemplo.
     */
    public static class MateriaRequest {
        @NotBlank(message = "El nombre es obligatorio")
        public String nombre;
        public String descripcion;
        public String color;
    }
}