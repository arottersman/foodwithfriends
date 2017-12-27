Organize potlucks

| Script  | Description                                    | Ports |
|---------|------------------------------------------------|-------|
| server  | start the development server                   | 8080  |
| bundle  | bundle the client app for deployment           |       |
| infra   | deploy infrastructure                          |       |
| console | access console for either the `service` or `db`|       |

Figwheel can be started from the clojurescript REPL:
```cljs
  (use 'figwheel-sidecar.repl-api)
  (fig-start)
  (cljs-repl)
```

To watch CSS changes:
```sh
cd client
lein garden auto
```
