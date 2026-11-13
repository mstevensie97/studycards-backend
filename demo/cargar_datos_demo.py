"""
Carga datos de demostración en StudyCards a través de la API del backend.

Uso (con el backend encendido):
    py cargar_datos_demo.py
    py cargar_datos_demo.py http://localhost:8080      (otra URL, por ejemplo la de Render)

- Usa solo la API, así que respeta las mismas validaciones que la app.
- Si una materia con el mismo nombre ya existe, la omite (se puede ejecutar dos veces).
- Deja las tarjetas en estados distintos para mostrar todo en la demo:
    * Cálculo Diferencial  -> 2 Aprendidas, 3 vencidas (Repasar) y 1 nueva
    * Historia Universal   -> todas nuevas (no vencidas): la sesión sale en orden aleatorio
    * Inglés Básico        -> 4 Aprendidas y 1 vencida
"""
import json
import sys
import urllib.error
import urllib.request

BASE_URL = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080").rstrip("/")

# nivel: None = se deja como nueva (vence en 5 días), "APRENDIDA" o "REPASAR" (vencida desde hoy)
DATOS = [
    {
        "nombre": "Cálculo Diferencial",
        "descripcion": "Límites, derivadas y sus aplicaciones",
        "color": "#1E88E5",
        "tarjetas": [
            ("¿Qué es la derivada de una función en un punto?",
             "La pendiente de la recta tangente a la gráfica de la función en ese punto. "
             "Mide la razón de cambio instantánea.", "APRENDIDA"),
            ("¿Cuál es la derivada de sen(x)?", "cos(x)", "APRENDIDA"),
            ("¿Qué dice la regla de la cadena?",
             "La derivada de f(g(x)) es f'(g(x)) · g'(x): se deriva la función externa "
             "y se multiplica por la derivada de la interna.", "REPASAR"),
            ("¿Qué es el límite de una función en un punto?",
             "El valor al que se acerca f(x) cuando x se aproxima a ese punto, "
             "sin importar el valor que tenga la función en el punto mismo.", "REPASAR"),
            ("¿Cuál es la derivada de x elevado a n?",
             "n · x elevado a (n - 1). Por ejemplo, la derivada de x³ es 3x².", "REPASAR"),
            ("¿Cuándo una función es continua en un punto a?",
             "Cuando f(a) existe, el límite de f(x) cuando x tiende a a existe, "
             "y ambos valores son iguales.", None),
        ],
    },
    {
        "nombre": "Historia Universal",
        "descripcion": "Grandes procesos y fechas clave",
        "color": "#FB8C00",
        "tarjetas": [
            ("¿En qué año comenzó la Revolución Francesa?",
             "En 1789, con la toma de la Bastilla el 14 de julio.", None),
            ("¿Qué fue la Revolución Industrial?",
             "Proceso iniciado en Gran Bretaña a mediados del siglo XVIII que pasó "
             "de la producción artesanal a la producción mecanizada en fábricas.", None),
            ("¿Qué dos bloques se enfrentaron en la Guerra Fría?",
             "Estados Unidos y sus aliados (OTAN) frente a la Unión Soviética "
             "y sus aliados (Pacto de Varsovia).", None),
            ("¿En qué año cayó el Muro de Berlín?", "En 1989, el 9 de noviembre.", None),
            ("¿Qué fue el Renacimiento?",
             "Movimiento cultural europeo (siglos XIV al XVI) que recuperó el interés "
             "por la cultura clásica y puso al ser humano en el centro del pensamiento.", None),
        ],
    },
    {
        "nombre": "Inglés Básico",
        "descripcion": "Vocabulario y estructuras esenciales",
        "color": "#43A047",
        "tarjetas": [
            ("¿Cómo se dice \"biblioteca\" en inglés?", "Library", "APRENDIDA"),
            ("¿Cuál es el pasado simple del verbo \"go\"?", "Went", "APRENDIDA"),
            ("¿Cuál es la diferencia entre \"much\" y \"many\"?",
             "\"Many\" se usa con sustantivos contables (many books); "
             "\"much\" con incontables (much water).", "APRENDIDA"),
            ("¿Qué significa \"I used to play football\"?",
             "Solía jugar fútbol: describe un hábito del pasado que ya no se mantiene.",
             "APRENDIDA"),
            ("¿Cómo se forma el presente continuo?",
             "Sujeto + verbo to be (am, is, are) + verbo terminado en -ing. "
             "Ejemplo: She is studying.", "REPASAR"),
        ],
    },
]


def llamar(metodo, ruta, cuerpo=None):
    datos = None if cuerpo is None else json.dumps(cuerpo, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(BASE_URL + ruta, data=datos, method=metodo)
    if datos is not None:
        req.add_header("Content-Type", "application/json; charset=utf-8")
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            texto = resp.read().decode("utf-8")
            return json.loads(texto) if texto else None
    except urllib.error.HTTPError as e:
        detalle = e.read().decode("utf-8", errors="replace")
        sys.exit(f"Error {e.code} en {metodo} {ruta}: {detalle}")
    except urllib.error.URLError as e:
        sys.exit(f"No se pudo conectar con {BASE_URL}: {e.reason}\n"
                 "Verifica que el backend esté encendido.")


def main():
    print(f"Cargando datos de demostración en {BASE_URL}\n")
    existentes = {m["nombre"] for m in llamar("GET", "/materias")}

    for materia in DATOS:
        if materia["nombre"] in existentes:
            print(f"- {materia['nombre']}: ya existe, se omite")
            continue

        creada = llamar("POST", "/materias", {
            "nombre": materia["nombre"],
            "descripcion": materia["descripcion"],
            "color": materia["color"],
        })
        mid = creada["id"]
        print(f"- {materia['nombre']} (id {mid})")

        for pregunta, respuesta, nivel in materia["tarjetas"]:
            t = llamar("POST", f"/materias/{mid}/tarjetas",
                       {"pregunta": pregunta, "respuesta": respuesta})
            if nivel:
                llamar("PUT", f"/materias/{mid}/tarjetas/{t['id']}/nivel",
                       {"nivelDominio": nivel})
        print(f"    {len(materia['tarjetas'])} tarjetas creadas")

    print("\nListo.")


if __name__ == "__main__":
    main()
