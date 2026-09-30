# Lovoe branding

Este repositório é o [nextcloud/android](https://github.com/nextcloud/android) com o branding Lovoe
aplicado como um **product flavor** isolado (`lovoe`). Todo o resto é código upstream sem alterações.

## Onde fica cada coisa

| O quê | Onde |
|---|---|
| Flavor, `applicationId` (`com.empresa.lovoe`) | `app/build.gradle.kts`, bloco `// region Lovoe` (única alteração em arquivo upstream) |
| Nome, cores, servidor travado, authorities, flags | `app/src/lovoe/res/values/setup.xml` |
| Textos que citam "Nextcloud" | `app/src/lovoe/res/values/strings.xml` e `values-pt-rBR/strings.xml` |
| Ícones e logos | `app/src/lovoe/res/drawable/`, `app/src/lovoe/res/mipmap-*/` (ver abaixo) |
| Código de variante (push, review, DI) | reaproveitado de `app/src/generic/java` (sem Firebase) |

Qualquer recurso colocado em `app/src/lovoe/res/` com o mesmo nome de um recurso de `app/src/main/res/`
sobrescreve o upstream apenas no build Lovoe.

Os arquivos originais da marca ficam em `branding/`. Os drawables foram vetorizados a partir deles:

| Drawable | Origem | Uso |
|---|---|---|
| `ic_launcher_foreground.xml` | símbolo de `ICONE_LOVOE.svg`, dentro da zona segura de 66dp | ícone do app, splash screen, ícone temático |
| `ic_launcher_background.xml` | Cosmic Aurora `#0F1A2F` | fundo do ícone |
| `notification_icon.xml` | símbolo com traço engrossado para 24dp | barra de status |
| `logo.xml` / `nextcloud_logo.xml` | `LOGO_LOVOE.svg` | tela de login e topo do menu lateral |
| `mipmap-*/ic_launcher.png`, `ic_launcher-web.png` | `ICONE_LOJA.png` | fallback legado e ícone da Play Store |

Os SVGs exportados do Canva trazem o símbolo como imagem embutida, que o Android não aceita em
`VectorDrawable`. Para trocar a arte, prefira exportar SVG com todas as formas em vetor.

## Servidor travado

`webview_login_url` aponta para `https://cloud.lovoe.online/index.php/login/flow`, com
`show_server_url_input=false` e `show_provider_or_own_installation=false`. O login abre direto no fluxo web
do servidor, e logins por QR code/deep link para outros hosts são recusados
(`AuthenticatorActivity.checkAllowedServers()`).

## Cores

A paleta vem de `setup.xml` (`primary` = Cosmic Aurora `#0F1A2F`, `color_accent` = Plasma Pink `#FF073A`).
Depois do login, o app usa a cor do app **Theming** do servidor. Configure a mesma cor primária no Theming
de `cloud.lovoe.online` para manter a identidade dentro do app.

## Build

```bash
./gradlew :app:assembleLovoeDebug     # debug
./gradlew :app:assembleLovoeRelease   # release (configure a assinatura)
```

## Atualizando a partir do upstream

```bash
git remote add upstream https://github.com/nextcloud/android.git   # uma vez
git fetch upstream master
git merge upstream/master
```

Os conflitos só podem aparecer no bloco `// region Lovoe` de `app/build.gradle.kts`. Depois de cada merge,
confira se `app/src/generic/java` ganhou arquivos novos (eles passam a valer para o Lovoe automaticamente) e se
`app/src/main/res/values/strings.xml` ganhou novas strings com "Nextcloud":

```bash
grep -n 'Nextcloud' app/src/main/res/values/strings.xml | grep -v 'translatable="false"'
```

### Workflows do GitHub Actions

Os workflows do upstream foram removidos. Fica só `.github/workflows/lovoe-apk.yml`, que gera o APK
(Actions → "Lovoe APK" → Artifacts). Se um merge do upstream trouxer conflito do tipo
"deleted by us" em `.github/workflows/`, mantenha a remoção:

```bash
git status --porcelain .github/workflows | awk '/^(DU|UD) /{print $2}' | xargs -r git rm -q
```
