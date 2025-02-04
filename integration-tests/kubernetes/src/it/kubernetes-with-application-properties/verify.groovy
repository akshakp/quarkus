import io.dekorate.utils.Serialization
import io.dekorate.deps.kubernetes.api.model.*
import io.dekorate.deps.kubernetes.api.model.apps.Deployment;

//Check that file exits
String base = basedir
File kubernetesYml = new File(base, "target/kubernetes/kubernetes.yml")
assert kubernetesYml.exists()

//Check that its parse-able
KubernetesList list = Serialization.unmarshal(kubernetesYml.text, KubernetesList.class)
assert list != null

//Check that ti contains a Deployment named after the project
Deployment deployment = list.items.find{r -> r.kind == "Deployment"}
assert deployment != null
assert deployment.metadata.name == "test-it"

//Check that deployment has label foo/bar
Map labels = deployment.metadata.labels
assert labels.containsKey("foo")
assert labels.get("foo") == "bar"

//Check that container has an env var named some value.
Container container = deployment.spec.template.spec.containers.get(0)
EnvVar env = container.env.find{e -> e.name == "MY_ENV_VAR"}
assert env != null
assert env.value == "SOMEVALUE"
