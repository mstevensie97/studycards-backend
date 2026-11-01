package co.studycards.materia;

import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

    public record TarjetaResponse(Long id,
                                  String pregunta,
                                  String respuesta,
                                  NivelDominio nivelDominio,
                                  Long materiaId) {
        static TarjetaResponse de(Tarjeta t) {
            return new TarjetaResponse(t.id, t.pregunta, t.respuesta, t.nivelDominio, t.materia.id);
        }
    }

    public record ListadoTarjetasResponse(Long materiaId,
                                          String materia,
                                          long total,
                                          List<TarjetaResponse> tarjetas) {
    }
}
