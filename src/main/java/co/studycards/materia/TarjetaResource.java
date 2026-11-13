package co.studycards.materia;

import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Path("/materias/{materiaId}/tarjetas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TarjetaResource {

    /**
     * HU008: crear una tarjeta manual (pregunta y respuesta) dentro de una materia.
     * POST /materias/{materiaId}/tarjetas
     * - 201 con la tarjeta creada (nivelDominio inicia en REPASAR).
     * - 400 si la pregunta o la respuesta están vacías o superan el largo permitido.
     * - 404 si la materia no existe.
     */
    @POST
    @Transactional
    public Response crear(@PathParam("materiaId") Long materiaId, @Valid TarjetaRequest request) {
        Materia materia = Materia.findById(materiaId);
        if (materia == null) {
            return materiaNoEncontrada(materiaId);
        }

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.pregunta = request.pregunta.trim();
        tarjeta.respuesta = request.respuesta.trim();
        tarjeta.materia = materia;
        tarjeta.fechaProximoRepaso = LocalDate.now().plusDays(Tarjeta.DIAS_PARA_REPASO);
        tarjeta.persist();

        return Response.status(Response.Status.CREATED).entity(TarjetaResponse.de(tarjeta)).build();
    }

    /**
     * HU011: ver el listado completo de tarjetas de una materia.
     * GET /materias/{materiaId}/tarjetas
     * - 200 con la lista (vacía si aún no tiene tarjetas), ordenada por creación.
     * - 404 si la materia no existe.
     */
    @GET
    public Response listar(@PathParam("materiaId") Long materiaId) {
        Materia materia = Materia.findById(materiaId);
        if (materia == null) {
            return materiaNoEncontrada(materiaId);
        }

        List<TarjetaResponse> tarjetas = Tarjeta.<Tarjeta>list("materia.id", Sort.by("id"), materiaId)
                .stream()
                .map(TarjetaResponse::de)
                .toList();

        return Response.ok(new ListadoTarjetasResponse(materia.id, materia.nombre, tarjetas.size(), tarjetas))
                .build();
    }

    /**
     * HU009: editar la pregunta y la respuesta de una tarjeta existente.
     * PUT /materias/{materiaId}/tarjetas/{tarjetaId}
     * - 200 con la tarjeta actualizada (el nivel de dominio no cambia).
     * - 400 si la pregunta o la respuesta están vacías o superan el largo permitido.
     * - 404 si la materia o la tarjeta no existen, o la tarjeta no es de esa materia.
     */
    @PUT
    @Path("/{tarjetaId}")
    @Transactional
    public Response actualizar(@PathParam("materiaId") Long materiaId,
                               @PathParam("tarjetaId") Long tarjetaId,
                               @Valid TarjetaRequest request) {
        Materia materia = Materia.findById(materiaId);
        if (materia == null) {
            return materiaNoEncontrada(materiaId);
        }

        Tarjeta tarjeta = Tarjeta.findById(tarjetaId);
        if (tarjeta == null || !tarjeta.materia.id.equals(materiaId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MateriaResource.ErrorResponse(
                            "Tarjeta con id " + tarjetaId + " no encontrada en la materia " + materiaId))
                    .build();
        }

        tarjeta.pregunta = request.pregunta.trim();
        tarjeta.respuesta = request.respuesta.trim();

        return Response.ok(TarjetaResponse.de(tarjeta)).build();
    }

    /**
     * HU010: eliminar una tarjeta individual.
     * DELETE /materias/{materiaId}/tarjetas/{tarjetaId}
     * - 204 si se eliminó (la materia y sus demás tarjetas no se tocan).
     * - 404 si la materia o la tarjeta no existen, o la tarjeta no es de esa materia.
     */
    @DELETE
    @Path("/{tarjetaId}")
    @Transactional
    public Response eliminar(@PathParam("materiaId") Long materiaId,
                             @PathParam("tarjetaId") Long tarjetaId) {
        Materia materia = Materia.findById(materiaId);
        if (materia == null) {
            return materiaNoEncontrada(materiaId);
        }

        Tarjeta tarjeta = Tarjeta.findById(tarjetaId);
        if (tarjeta == null || !tarjeta.materia.id.equals(materiaId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MateriaResource.ErrorResponse(
                            "Tarjeta con id " + tarjetaId + " no encontrada en la materia " + materiaId))
                    .build();
        }

        RegistroEstudio.delete("tarjetaId", tarjetaId);
        tarjeta.delete();
        return Response.noContent().build();
    }

    /**
     * HU020 + HU021: marcar una tarjeta como APRENDIDA o como REPASAR.
     * PUT /materias/{materiaId}/tarjetas/{tarjetaId}/nivel
     * Body: {"nivelDominio": "APRENDIDA"} o {"nivelDominio": "REPASAR"}
     * - 200 con la tarjeta actualizada.
     *   Al marcarla REPASAR queda vencida desde hoy, para que vuelva a salir
     *   en las próximas sesiones de estudio (HU013).
     * - 400 si el nivel no es APRENDIDA ni REPASAR.
     * - 404 si la materia o la tarjeta no existen, o la tarjeta no es de esa materia.
     */
    @PUT
    @Path("/{tarjetaId}/nivel")
    @Transactional
    public Response actualizarNivel(@PathParam("materiaId") Long materiaId,
                                    @PathParam("tarjetaId") Long tarjetaId,
                                    NivelRequest request) {
        Materia materia = Materia.findById(materiaId);
        if (materia == null) {
            return materiaNoEncontrada(materiaId);
        }

        Tarjeta tarjeta = Tarjeta.findById(tarjetaId);
        if (tarjeta == null || !tarjeta.materia.id.equals(materiaId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MateriaResource.ErrorResponse(
                            "Tarjeta con id " + tarjetaId + " no encontrada en la materia " + materiaId))
                    .build();
        }

        NivelDominio nuevoNivel = null;
        if (request != null && request.nivelDominio != null) {
            try {
                nuevoNivel = NivelDominio.valueOf(request.nivelDominio.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                nuevoNivel = null;
            }
        }
        if (nuevoNivel == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MateriaResource.ErrorResponse(
                            "El nivel de dominio debe ser APRENDIDA o REPASAR"))
                    .build();
        }

        tarjeta.nivelDominio = nuevoNivel;
        if (nuevoNivel == NivelDominio.REPASAR) {
            tarjeta.fechaProximoRepaso = LocalDate.now();
        }

        // RQ-16: registrar la respuesta en el historial de estudio.
        RegistroEstudio registro = new RegistroEstudio();
        registro.materiaId = materiaId;
        registro.tarjetaId = tarjetaId;
        registro.resultado = nuevoNivel;
        registro.tiempoSegundos =
                (request.tiempoSegundos != null && request.tiempoSegundos >= 0)
                        ? request.tiempoSegundos : null;
        registro.fecha = LocalDateTime.now();
        registro.persist();

        return Response.ok(TarjetaResponse.de(tarjeta)).build();
    }

    private Response materiaNoEncontrada(Long materiaId) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new MateriaResource.ErrorResponse("Materia con id " + materiaId + " no encontrada"))
                .build();
    }

    public static class TarjetaRequest {
        @NotBlank(message = "La pregunta es obligatoria")
        @Size(max = 500, message = "La pregunta no puede superar 500 caracteres")
        public String pregunta;

        @NotBlank(message = "La respuesta es obligatoria")
        @Size(max = 1000, message = "La respuesta no puede superar 1000 caracteres")
        public String respuesta;
    }

    public static class NivelRequest {
        public String nivelDominio;
        /** RQ-16: segundos que tardó el estudiante en responder (opcional). */
        public Integer tiempoSegundos;
    }

    public record TarjetaResponse(Long id,
                                  String pregunta,
                                  String respuesta,
                                  NivelDominio nivelDominio,
                                  Long materiaId,
                                  LocalDate fechaProximoRepaso) {
        static TarjetaResponse de(Tarjeta t) {
            return new TarjetaResponse(t.id, t.pregunta, t.respuesta, t.nivelDominio, t.materia.id,
                    t.fechaProximoRepaso);
        }
    }

    public record ListadoTarjetasResponse(Long materiaId,
                                          String materia,
                                          long total,
                                          List<TarjetaResponse> tarjetas) {
    }
}