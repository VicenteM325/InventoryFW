export function filterRoutesByRole(routes, roles = []) {
    if (!Array.isArray(roles)) {
        roles = [];
    }

    console.log('=== filterRoutesByRole ===');
    console.log('Roles recibidos:', roles);
    console.log('Routes originales:', routes);

    const result = routes
        .map(section => ({
            ...section,
            pages: section.pages.filter(page => {
                // Filtrar por hidden para el menú
                if (page.hidden) return false;
                
                // Filtrar por roles
                if (!page.roles) {
                    console.log(`Página ${page.name} no tiene roles, se incluye`);
                    return true;
                }
                if (!Array.isArray(page.roles)) {
                    console.log(`Página ${page.name} tiene roles pero no es array`);
                    return false;
                }
                const hasRole = page.roles.some(role => roles.includes(role));
                console.log(`Página ${page.name} - roles requeridos: ${page.roles.join(', ')} - tiene rol? ${hasRole}`);
                return hasRole;
            })
        }))
        .filter(section => section.pages.length > 0);

    console.log('Resultado filtrado:', result);
    return result;
}

export function filterRoutesForNavigation(routes, roles = []) {
    if (!Array.isArray(roles)) {
        roles = [];
    }

    return routes
        .map(section => ({
            ...section,
            pages: section.pages.filter(page => {
                if (!page.roles) return true;
                if (!Array.isArray(page.roles)) return false;
                return page.roles.some(role => roles.includes(role));
            })
        }))
        .filter(section => section.pages.length > 0);
}

export function getHomeByRole(roles = []) {
  console.log('=== getHomeByRole ===');
  console.log('Roles recibidos:', roles);
  
  if (roles.includes("ROLE_ADMIN"))
    return "/dashboard/home";

  if (roles.includes("ROLE_EMPLOYEE"))
    return "/dashboard/admin";

  // Si no tiene ningún rol conocido, ir a home
  return "/dashboard/home";
}