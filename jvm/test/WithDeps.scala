import play.api.test.{WithApplicationLoader, WithBrowser}

class WithDepsApplication() extends WithApplicationLoader(new WebAppLoader())

class WithDepsBrowser() extends WithBrowser(app = new WithDepsApplication().app)
