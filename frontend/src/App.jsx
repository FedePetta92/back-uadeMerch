import Layout from './components/Layout'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import CategoryList from './components/CategoryList'
import ProductList from './components/ProductList'
import QuantitySelector from './components/QuantitySelector'
import { productos } from './data/productos'
import { categorias } from './data/categorias'

function App() {
  return (
    <Layout>
      <Navbar />
      <CategoryList categorias={categorias} />
      <ProductList productos={productos} />

      {/* Demo temporal */}
      <div className="p-3">
        <p className="mb-2">Demo QuantitySelector (stock máx: 10):</p>
        <QuantitySelector stockMax={10} onCambiar={(n) => console.log('cantidad:', n)} />
      </div>

      <Footer />
    </Layout>
  )
}

export default App
