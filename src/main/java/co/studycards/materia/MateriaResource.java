package co.studycards.materia;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/materias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MateriaResource {

    /**
     * Listar todas las materias.
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
     * HU003: eliminar una materia que el estudiante ya no va a seguir estudiando.
     * DELETE /materias/{id}
     * - 204 si se eliminó correctamente.
     * - 404 si la materia no existe.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response eliminar(@PathParam("id") Long id) {
        boolean eliminada = Materia.deleteById(id);
        if (!eliminada) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Materia con id " + id + " no encontrada"))
                    .build();
        }
        return Response.noContent().build();
    }

    /**
     * DTO de entrada para crear materias.
     */
    public static class MateriaRequest {
        @NotBlank(message = "El nombre es obligatorio")
        public String nombre;
        public String descripcion;
        public String color;
    }

    /**
     * Respuesta estándar de error.
     */
    public record ErrorResponse(String mensaje) {
    }
}