1. Copy resources to your Laravel install.

2. Copy josso-php-agent resources to app/Josso folder from PHP Agent.

3. Modify bootstrap to add Josso folder to included resources. You may use another method.

Application Service Provider boot:

```
    /**
     * Bootstrap any application services.
     *
     * @return void
     */
    public function boot()
    {
        // Modified to add Josso to the PHP include path!

        set_include_path(
            implode(
                ":",
                array(
                    '/var/www/html/sso-test/app/Josso',
                    get_include_path()
                )
            )
        );
    }
```
    
4. Define Middlewares

4.1 Web Middleware Group:

```    \App\Http\Middleware\JOSSOLaravelAgent::class,```

4.2 Route Middlewares

```
    'josso.acs' => \App\Http\Middleware\JOSSOAssertionConsumerService::class,
    'josso.login' => \App\Http\Middleware\JOSSOLogin::class,
    'josso.logout' => \App\Http\Middleware\JOSSOLogout::class,
```
 
5. Use in Route definitions

```
    Route::get('user-info', function () {
    
        $jossoagent = jossoagent::getInstance();
        $jossouser = $jossoagent->getUserInSession();
    
        return view('josso-user-info', ['josso_username' => $jossouser->getName()]);
    });
    
    Route::get('josso-security-check.php', function () {
        //
    })->middleware('josso.acs');
    
    
    Route::get('josso-login', function () {
        //
    })->middleware('josso.login');
    
    
    Route::get('josso-logout', function () {
        //
    })->middleware('josso.logout');
```         
            