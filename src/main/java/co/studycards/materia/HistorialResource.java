package co.studycards.materia;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;

@Path("/materias/{materiaId}/historial")
@Produces(MediaType.APPLICATION_JSON)
public class HistorialResource {

    private static final int MAXIMO_REGISTROS = 200;

    /**
     * RQ-16: consultar el historial de estudio de una materia.
     * GET /materias/{materiaId}/historial
     * - 200 con los registros más recientes primero (máximo 200).
     * - 404 si la materia no existe.
     */
    @GET
    public Response listar(@PathParam("materiaId") Long materiaId) {
        Materia materia = Materia.findById(materiaId);
        if (materia == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MateriaResource.ErrorResponse(
                            "Materia con id " + materiaId + " no encontrada"))
                    .build();
        }

        long total = RegistroEstudio.count("materiaId", materiaId);
        List<RegistroResponse> registros = RegistroEstudio
                .<RegistroEstudio>list("materiaId", Sort.by("fecha").descending(), materiaId)
                .stream()
                .limit(MAXIMO_REGISTROS)
                .map(r -> new RegistroResponse(r.id, r.tarjetaId, r.resultado, r.tiempoSegundos, r.fecha))
                .toList();

        return Response.ok(new HistorialResponse(materia.id, materia.nombre, total, registros)).build();
    }

    public record RegistroResponse(Long id,
                                   Long tarjetaId,
                                   NivelDominio resultado,
                                   Integer tiempoSegundos,
                                   LocalDateTime fecha) {
    }

    public record HistorialResponse(Long materiaId,
                                    String materia,
                                    long total,
                                    List<RegistroResponse> registros) {
    }
}