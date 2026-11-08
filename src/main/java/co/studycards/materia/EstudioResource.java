package co.studycards.materia;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/materias/{materiaId}/estudio")
@Produces(MediaType.APPLICATION_JSON)
public class EstudioResource {

    /**
     * HU012: iniciar una sesión de estudio de una materia.
     * GET /materias/{materiaId}/estudio
     * - 200 con las tarjetas de la materia en orden aleatorio
     *   (la lista viene vacía si la materia todavía no tiene tarjetas).
     * - 404 si la materia no existe.
     *
     * En la HU013 este endpoint pasará a elegir primero las tarjetas vencidas.
     */
    @GET
    public Response iniciar(@PathParam("materiaId") Long materiaId) {
        Materia materia = Materia.findById(materiaId);
        if (materia == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MateriaResource.ErrorResponse(
                            "Materia con id " + materiaId + " no encontrada"))
                    .build();
        }

        List<TarjetaResource.TarjetaResponse> tarjetas = new ArrayList<>(
                Tarjeta.<Tarjeta>list("materia.id", Sort.by("id"), materiaId)
                        .stream()
                        .map(TarjetaResource.TarjetaResponse::de)
                        .toList());
        Collections.shuffle(tarjetas);

        return Response.ok(new SesionEstudioResponse(
                materia.id, materia.nombre, tarjetas.size(), tarjetas)).build();
    }

    public record SesionEstudioResponse(Long materiaId,
                                        String materia,
                                        int total,
                                        List<TarjetaResource.TarjetaResponse> tarjetas) {
    }
}