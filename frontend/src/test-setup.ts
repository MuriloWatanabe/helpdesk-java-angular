/**
 * Garante um `localStorage` funcional nos testes.
 * Em versões recentes do Node, o `localStorage` nativo sobrescreve o do jsdom
 * e fica indefinido quando o runtime não recebe `--localstorage-file`.
 */
function criarStorageEmMemoria(): Storage {
  const itens = new Map<string, string>();
  return {
    get length() { return itens.size; },
    clear: () => itens.clear(),
    getItem: (chave) => itens.get(chave) ?? null,
    key: (indice) => [...itens.keys()][indice] ?? null,
    removeItem: (chave) => { itens.delete(chave); },
    setItem: (chave, valor) => { itens.set(chave, String(valor)); },
  };
}

if (typeof globalThis.localStorage?.clear !== 'function') {
  Object.defineProperty(globalThis, 'localStorage', {
    value: criarStorageEmMemoria(),
    configurable: true,
  });
}
