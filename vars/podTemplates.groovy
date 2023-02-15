def dockerTemplate(Closure body) {
  podTemplate(
    containers: [containerTemplate(name: 'docker', image: 'docker:19.03', command: 'cat', ttyEnabled: true)],
    volumes: [hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')]
  ) {
    body()
  }
}


def helmTemplate(Closure body) {
  podTemplate(
    containers: [
      containerTemplate(
        name: 'helm',
        image: 'alpine/helm:3.5.0',
        command: 'cat',
        ttyEnabled: true,
        runAsUser: '1000',
        envVars: [
          envVar(key: 'HOME', value: '/tmp') // Temporarily setting this so the helm-push plugin and helm repos aren't cached
        ]
      )
    ]
  ) {
    body()
  }
}

def pythonTemplate(Closure body) {
  podTemplate(
    containers: [
      containerTemplate(
        name: 'python',
        image: 'python:latest',
        command: 'cat',
        ttyEnabled: true
      )
    ]
  ) {
    body()
  }
}


