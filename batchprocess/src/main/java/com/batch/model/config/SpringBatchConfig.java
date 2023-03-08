package com.batch.model.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.batch.model.Customer;
import com.batch.model.repository.CustomerRepository;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

	private JobBuilderFactory jobBuilder;
	
	private StepBuilderFactory stepBuilder;
	
	private CustomerRepository customerRepository;
	
	public SpringBatchConfig(JobBuilderFactory jobBuilder, StepBuilderFactory stepBuilder,
			CustomerRepository customerRepository) {
		super();
		this.jobBuilder = jobBuilder;
		this.stepBuilder = stepBuilder;
		this.customerRepository = customerRepository;
	}
	
	@Bean
	public FlatFileItemReader<Customer> reader(){
		FlatFileItemReader<Customer> itemReader=new FlatFileItemReader<>();
		itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
		itemReader.setName("csvReader");
		itemReader.setLinesToSkip(1);
		itemReader.setLineMapper(lineMapper());
		return itemReader;
	}

	private LineMapper<Customer> lineMapper() {
		DefaultLineMapper<Customer> lineMapper=new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id", "firstname", "lastname", "age");
		
		BeanWrapperFieldSetMapper<Customer> fieldSetMapper=new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(Customer.class);
		
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		
		return lineMapper;
	}
	
	@Bean
	public CustomerProcessor processor() {
		return new CustomerProcessor();
	}
	
	@Bean
	public ItemWriter<Customer> writer(){
		RepositoryItemWriter<Customer> writer=new RepositoryItemWriter<>();
		writer.setRepository(customerRepository);
		writer.setMethodName("save");
		return writer;
	}
	
	@Bean
	public Step step1() {
		return stepBuilder.get("csv-step").<Customer, Customer>chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.taskExecutor(taskExecutor())
				.build();
	}
	
	@Bean	
	public Job runJob() {
		return jobBuilder.get("import Customers")
				.flow(step1()).end().build();
	}
	
	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor asyncTaskExecutor=new SimpleAsyncTaskExecutor();
		asyncTaskExecutor.setConcurrencyLimit(10);
		return asyncTaskExecutor;
	}

}
