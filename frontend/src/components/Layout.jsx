// props: children (ReactNode)
function Layout({ children }) {
  return (
    <div className="container-fluid px-0">
      {children}
    </div>
  )
}

export default Layout
