/**
 * No build de produção o front é servido pelo nginx, que repassa /api para o
 * backend (ver nginx.conf). Usar caminho relativo evita fixar o host da API na
 * imagem e dispensa CORS entre os contêineres.
 */
export const environment = {
  production: true,
  apiUrl: '/api',
};
