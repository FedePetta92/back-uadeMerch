import { useState } from 'react'

// props: stockMax (number), onCambiar (fn opcional que recibe la cantidad nueva)
function QuantitySelector({ stockMax, onCambiar }) {
  const [cantidad, setCantidad] = useState(1)

  function restar() {
    const nueva = cantidad - 1
    setCantidad(nueva)
    if (onCambiar) onCambiar(nueva)
  }

  function sumar() {
    const nueva = cantidad + 1
    setCantidad(nueva)
    if (onCambiar) onCambiar(nueva)
  }

  return (
    <div className="input-group" style={{ width: '130px' }}>
      <button
        className="btn btn-outline-secondary"
        onClick={restar}
        disabled={cantidad <= 1}
      >
        −
      </button>
      <span className="form-control text-center">{cantidad}</span>
      <button
        className="btn btn-outline-secondary"
        onClick={sumar}
        disabled={cantidad >= stockMax}
      >
        +
      </button>
    </div>
  )
}

export default QuantitySelector
