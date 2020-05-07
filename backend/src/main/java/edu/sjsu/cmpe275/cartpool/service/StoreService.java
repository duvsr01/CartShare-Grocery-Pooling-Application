package edu.sjsu.cmpe275.cartpool.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import edu.sjsu.cmpe275.cartpool.Constants;
import edu.sjsu.cmpe275.cartpool.dto.Order;
import edu.sjsu.cmpe275.cartpool.dto.Product;
import edu.sjsu.cmpe275.cartpool.dto.Store;
import edu.sjsu.cmpe275.cartpool.repository.OrderRepository;
import edu.sjsu.cmpe275.cartpool.repository.ProductRepository;
import edu.sjsu.cmpe275.cartpool.repository.StoreRepository;

@Service
public class StoreService {

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductService productService;

	public List<Store> getAllStores() {
		return storeRepository.findAll();
	}

	@Transactional
	public Store createStore(Store store) {
		if (storeRepository.existsByName(store.getName())) {
			Store existingStore = storeRepository.findStoreByName(store.getName());
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Store already exists");
		}
		return storeRepository.save(store);
	}

	@Transactional
	public Store updateStore(Store store) {
		Store existingStore = storeRepository.findStoreByName(store.getName());
		if (existingStore == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		existingStore.setAddress(store.getAddress());
		Store response = storeRepository.save(existingStore);
		return response;
	}

	@Transactional
	public Store deleteStore(long storeId) {
		Optional<Store> existingStore = storeRepository.findById(storeId);
		if (!existingStore.isPresent()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
		}
		Store store = existingStore.get();
		String storeName = store.getName();
		List<Order> orders = orderRepository.findByStoreName(storeName);
		if (orders != null && !orders.isEmpty()) {
			for (Order order : orders) {
				if (Constants.PLACED.equals(order.getStatus()) && (Constants.ASSIGNED.equals(order.getStatus()))) {

					throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
							"Cannot delete store because of unfulfilled orders");

				}
			}
		}
		List<Product> products = productRepository.findByStore(store);
		if (products != null && !products.isEmpty()) {
			for (Product product : products) {

				long productId = product.getId();

				productService.deleteProduct(productId);

			}

		}

		storeRepository.deleteById(storeId);
		return existingStore.get();
	}

}
