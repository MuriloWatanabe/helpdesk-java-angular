package com.murilo.helpdesk.model.enums;


public enum Categoria {
    REDE(0, "Problemas de Rede", "🌐"),
    HARDWARE(1, "Problemas de Hardware", "🖥️"),
    SOFTWARE(2, "Problemas de Software", "💻"),
    ACESSO(3, "Acesso/Permissões", "🔐"),
    EMAIL(4, "Problemas de Email", "📧"),
    VPN(5, "Problemas de VPN", "🔗"),
    IMPRESSORA(6, "Problemas de Impressora", "🖨️"),
    BACKUP(7, "Backup/Recuperação", "💾"),
    INSTALACAO(8, "Instalação de Software", "⬇️"),
    OUTRO(9, "Outro", "❓");

    private final Integer codigo;
    private final String descricao;
    private final String icone;

    Categoria(Integer codigo, String descricao, String icone) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.icone = icone;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getIcone() {
        return icone;
    }
}
