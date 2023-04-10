How to test flow locally?
1. FrontEndImpl-line:136-InetAddress.getByName(Constants.SQ_IP)=>InetAddress.getLocalHost()
2. Sequencer-line:72-InetAddress.getByName(Constants.FE_IP)=>InetAddress.getLocalHost()
3. ReplicaManager1-line:34-add after ms = new MulticastSocket(Constants.multicastSocket);
NetworkInterface networkInterface = NetworkInterface.getByName("en0");
Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();
ms.setNetworkInterface(networkInterface);
4. ReplicaManager1-line:84-InetAddress.getByName(Constants.FE_IP)=>InetAddress.getLocalHost()