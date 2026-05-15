require "json"

react_native_pods = Pod::Executable.execute_command(
  "node",
  [
    "-p",
    "require.resolve('react-native/scripts/react_native_pods.rb', { paths: [process.argv[1]] })",
    __dir__
  ]
).strip

require react_native_pods

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "munchi-sunmi-printer"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = "https://github.com/munchi/js-sdk"
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "13.0" }
  s.swift_versions = ["5.0"]
  s.source       = { :git => "https://github.com/munchi/js-sdk.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift}"

  if respond_to?(:install_modules_dependencies, true)
    install_modules_dependencies(s)
  else
    s.dependency "React-Core"
  end
end
