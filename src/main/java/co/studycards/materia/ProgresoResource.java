package co.studycards.materia;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashSet;
import java.util.Set;

@Path("/materias/{materiaId}/progreso")
@Produces(MediaType.APPLICATION_JSON)
public class ProgresoResource {

    /**
     * RQ-15: progreso de estudio de una materia.
     * GET /materias/{materiaId}/progreso
     * - 200 con el total de tarjetas, cuántas se han estudiado y qué porcentaje
     *   está marcado como APRENDIDA.
     * - 404 si la materia no existe.
     *
     * Una tarjeta cuenta como "estudiada" si tiene algún registro en el
     * historial de estudio o si ya está marcada como APRENDIDA.
     */
    @GET
    public Response consultar(@PathParam("materiaId") Long materiaId) {
        Materia materia = Materia.findById(materiaId);
        if (materia == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MateriaResource.ErrorResponse(
                            "Materia con id " + materiaId + " no encontrada"))
                    .build();
        }

        long total = Tarjeta.count("materia.id", materiaId);
        long aprendidas = Tarjeta.count(
                "materia.id = ?1 and nivelDominio = ?2", materiaId, NivelDominio.APRENDIDA);

        Set<Long> estudiadas = new HashSet<>(
                RegistroEstudio.getEntityManager()
                        .createQuery("select distinct r.tarjetaId from RegistroEstudio r "
                                + "where r.materiaId = :id", Long.class)
                        .setParameter("id", materiaId)
                        .getResultList());
        Tarjeta.<Tarjeta>list("materia.id = ?1 and nivelDominio = ?2",
                materiaId, NivelDominio.APRENDIDA).forEach(t -> estudiadas.add(t.id));

        int porcentaje = total == 0 ? 0 : (int) Math.round(aprendidas * 100.0 / total);

        return Response.ok(new ProgresoResponse(
                materia.id, materia.nombre, total, estudiadas.size(), aprendidas, porcentaje)).build();
    }

    public record ProgresoResponse(Long materiaId,
                                   String materia,
                                   long totalTarjetas,
                                   long estudiadas,
                                   long aprendidas,
                                   int porcentajeAprendidas) {
    }
}