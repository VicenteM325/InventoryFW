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
  if (roles.includes("ROLE_ADMIN"))
    return "/dashboard/home";

  if (roles.includes("ROLE_EMPLOYEE"))
    return "/dashboard/sales";

  // Si no tiene ningún rol conocido, ir a home
  return "/dashboard/home";
}
