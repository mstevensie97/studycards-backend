package co.studycards.materia;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/materias/{materiaId}/estudio")
@Produces(MediaType.APPLICATION_JSON)
public class EstudioResource {

    public static final String MODO_VENCIDAS = "VENCIDAS";
    public static final String MODO_ALEATORIO = "ALEATORIO";

    /**
     * HU012 + HU013: iniciar una sesión de estudio de una materia.
     * GET /materias/{materiaId}/estudio
     *
     * - Si hay tarjetas vencidas (en REPASAR y con la fecha de próximo repaso
     *   cumplida, o sin fecha), la sesión incluye solo esas (modo VENCIDAS).
     * - Si ninguna está vencida, la sesión incluye la materia completa en
     *   orden aleatorio (modo ALEATORIO).
     * - 404 si la materia no existe.
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

        List<Tarjeta> seleccion = Tarjeta.list(
                "materia.id = ?1 and nivelDominio = ?2 "
                        + "and (fechaProximoRepaso is null or fechaProximoRepaso <= ?3)",
                materiaId, NivelDominio.REPASAR, LocalDate.now());
        String modo = MODO_VENCIDAS;

        if (seleccion.isEmpty()) {
            seleccion = Tarjeta.list("materia.id", materiaId);
            modo = MODO_ALEATORIO;
        }

        List<TarjetaResource.TarjetaResponse> tarjetas = new ArrayList<>(
                seleccion.stream().map(TarjetaResource.TarjetaResponse::de).toList());
        Collections.shuffle(tarjetas);

        return Response.ok(new SesionEstudioResponse(
                materia.id, materia.nombre, modo, tarjetas.size(), tarjetas)).build();
    }

    public record SesionEstudioResponse(Long materiaId,
                                        String materia,
                                        String modo,
                                        int total,
                                        List<TarjetaResource.TarjetaResponse> tarjetas) {
    }
}