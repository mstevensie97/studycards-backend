package co.studycards.materia;

import io.quarkus.panache.common.Sort;
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
     * HU006: listar las materias del estudiante, ordenadas por nombre.
     * Por cada materia se devuelve el total de tarjetas y cuántas están
     * pendientes de repaso, para decidir de un vistazo por dónde empezar.
     * GET /materias
     */
    @GET
    public List<MateriaResumen> listar() {
        List<Materia> materias = Materia.listAll(Sort.by("nombre"));
        return materias.stream()
                .map(m -> new MateriaResumen(
                        m.id,
                        m.nombre,
                        m.descripcion,
                        m.color,
                        Tarjeta.count("materia.id", m.id),
                        Tarjeta.count("materia.id = ?1 and nivelDominio = ?2", m.id, NivelDominio.REPASAR)))
                .toList();
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
     * - 404 si no existe.
     * - 409 (advertencia) si tiene tarjetas y todavía no se confirmó.
     * - 204 si se eliminó (con ?confirmar=true se eliminan también sus tarjetas).
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
     * Materia con sus contadores de tarjetas (HU006).
     */
    public record MateriaResumen(Long id,
                                 String nombre,
                                 String descripcion,
                                 String color,
                                 long totalTarjetas,
                                 long tarjetasPendientes) {
    }

    public record ErrorResponse(String mensaje) {
    }

    public record AdvertenciaResponse(String mensaje, long totalTarjetas) {
    }
}