package co.studycards.materia;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
     * HU003 + HU004: eliminar una materia.
     * DELETE /materias/{id}
     *
     * - 404 si la materia no existe.
     * - 409 (advertencia) si tiene tarjetas y todavía NO se confirmó la eliminación.
     *   La respuesta incluye cuántas tarjetas se perderían, para mostrarlo en el front.
     * - 204 si se eliminó. Si tenía tarjetas, se envía ?confirmar=true y se
     *   eliminan junto con la materia.
     *
     * HU005 (cancelar): si el usuario cancela, el front simplemente no vuelve a
     * llamar con ?confirmar=true, así que la materia y sus tarjetas quedan intactas.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response eliminar(@PathParam("id") Long id,
                             @QueryParam("confirmar") @DefaultValue("false") boolean confirmar) {
        Materia materia = Materia.findById(id);
        if (materia == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Materia con id " + id + " no encontrada"))
                    .build();
        }

        long totalTarjetas = Tarjeta.count("materia.id", id);

        if (totalTarjetas > 0 && !confirmar) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new AdvertenciaResponse(
                            "La materia tiene " + totalTarjetas
                                    + " tarjeta(s) asociada(s). Si la eliminas, también se eliminarán.",
                            totalTarjetas))
                    .build();
        }

        if (totalTarjetas > 0) {
            Tarjeta.delete("materia.id", id);
        }
        materia.delete();
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

    /**
     * Respuesta de advertencia cuando la materia tiene tarjetas asociadas.
     */
    public record AdvertenciaResponse(String mensaje, long totalTarjetas) {
    }
}