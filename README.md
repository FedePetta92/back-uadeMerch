# UadeMerchAPI

Backend REST desarrollado en Spring Boot para la gestión del e-commerce de merchandising de la UADE. Permite administrar, autenticar y validar usuarios, productos, categorías y carritos de compra, incluyendo operaciones de alta, baja, modificación y consulta sobre cada uno de ellos.

El sistema persiste los datos en una base MySQL mediante JPA/Hibernate, y expone sus funcionalidades a través de endpoints HTTP consumibles desde clientes externos como por ejemplo Postman o Swagger.

El alcance actual cubre la lógica de negocio principal (catálogo de productos y carritos por usuario).
Cabe destacar que la autenticación, roles y medios de pago quedan fuera del alcance de esta primera entrega.