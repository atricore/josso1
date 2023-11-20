{
  description = "A Nix-flake-based Java 8 + Maven 3.5.4 dev env";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";

  outputs = {
    self,
    nixpkgs,
  }: let
    javaVersion = 8;
    overlays = [
      (final: prev: {
        jdk = prev."jdk${toString javaVersion}_headless";
        maven = prev.stdenv.mkDerivation {
          name = "maven-2.2.1";
          src = prev.fetchurl {
            url = "https://archive.apache.org/dist/maven/maven-2/2.2.1/binaries/apache-maven-2.2.1-bin.tar.gz";
            sha256 = "sha256-uaNlWUhqhiq/x/sgZP0UKfIDM8qulaxRIV0G1ywC03Y=";
          };
          buildInputs = [prev.maven];
          buildCommand = ''
            tar -xf $src
            mv apache-maven-2.2.1 $out
          '';
        };
      })
    ];
    supportedSystems = ["x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin"];
    forEachSupportedSystem = f:
      nixpkgs.lib.genAttrs supportedSystems (system:
        f {
          pkgs = import nixpkgs {inherit overlays system;};
        });
  in {
    devShells = forEachSupportedSystem ({pkgs}: {
      default = pkgs.mkShell {
        packages = with pkgs; [jdk maven];
        shellHook = ''
          echo "Atricore IDBus: Java dev env ("${pkgs.jdk.name}" / ${pkgs.maven.name})"
          export NIXPKGS_ALLOW_UNFREE=1

          export JAVA_HOME="${pkgs.jdk}"
          export MAVEN_HOME="${pkgs.maven}"
          export MAVEN_OPTS="-Dmaven.test.skip=true -Xmx2048m"

          if [ -z "$NON_NIX" ]; then
            export NON_NIX="/wa/3rdparty"
          fi

          echo "Using NON_NIX=$NON_NIX"

          export WEBLOGIC_10_HOME="$NON_NIX/weblogic/10"
          export WEBLOGIC_14_HOME="$NON_NIX/weblogic/14"
          export WEBLOGIC_12_HOME="$NON_NIX/weblogic/12"
          export WEBLOGIC_9_2_HOME="$NON_NIX/weblogic/9.2"
        '';
      };
    });
  };
}
